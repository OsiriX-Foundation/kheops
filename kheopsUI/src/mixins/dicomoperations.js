/* eslint-disable */
import { uuid } from 'vue-uuid';
import { HTTP } from '@/router/http';
import dicom from '@/mixins/dicom';
import customdicom from '@/mixins/customdicom';

export const DicomOperations = {
  data() {
    return {
      tagPhotographicImage: {
        necessaryTag: [
          '00100010',
          '00100020',
          '00100030',
          '00100040',
          '0020000D',
          '00080020',
          '00080030',
          '00080090',
          '00200010',
          '00080050',
          '00080005',
        ],
        createTag: [
          { tag: '00080060', vr: 'CS', value: ['XC'] },
          { tag: '0020000E', vr: 'UI', value: 'OID' },
          { tag: '00200011', vr: 'IS', value: '' },
          { tag: '00080070', vr: 'LO', value: '' },
          { tag: '7FE00010', vr: 'OB', value: 'BulkDataURI' },
          { tag: '00080016', vr: 'UI', value: ['1.2.840.10008.5.1.4.1.1.77.1.4'] },
          { tag: '00080018', vr: 'UI', value: 'OID' },
          { tag: '00200013', vr: 'IS', value: [1] },
          { tag: '00280002', vr: 'US', value: '' },
          { tag: '00280004', vr: 'CS', value: '' },
          { tag: '00280010', vr: 'US', value: '' },
          { tag: '00280011', vr: 'US', value: '' },
          { tag: '00280100', vr: 'US', value: '' },
          { tag: '00280101', vr: 'US', value: '' },
          { tag: '00280102', vr: 'US', value: '' },
          { tag: '00280103', vr: 'US', value: '' },
          { tag: '00282110', vr: 'CS', value: ['01'] },
          { tag: '00400555', vr: 'SQ', value: '' },
          { tag: '00080008', vr: 'CS', value: ['ORIGINAL', 'PRIMARY'] },
        ],
        tagBulkDataURI: '7FE00010',
        transferSyntax: '1.2.840.10008.1.2.4.50',
      },
      tagEncapsulatedPDF: {
        necessaryTag: [
          '00100010',
          '00100020',
          '00100030',
          '00100040',
          '0020000D',
          '00080020',
          '00080030',
          '00080090',
          '00200010',
          '00080050',
          '00080005',
        ],
        createTag: [
          { tag: '00080060', vr: 'CS', value: ['DOC'] },
          { tag: '0020000E', vr: 'UI', value: 'OID' },
          { tag: '00200011', vr: 'IS', value: '' },
          { tag: '00080070', vr: 'LO', value: '' },
          { tag: '00080064', vr: 'CS', value: ['SD'] },
          { tag: '00200013', vr: 'IS', value: [1] },
          { tag: '00080023', vr: 'DA', value: '' },
          { tag: '00080033', vr: 'TM', value: '' },
          { tag: '0008002A', vr: 'DT', value: '' },
          { tag: '00280301', vr: 'CS', value: ['YES'] },
          { tag: '00420010', vr: 'ST', value: '' },
          { tag: '0040A043', vr: 'SQ', value: '' },
          { tag: '00420012', vr: 'LO', value: ['application/pdf'] },
          { tag: '00420011', vr: 'OB', value: 'BulkDataURI' },
          { tag: '00080016', vr: 'UI', value: ['1.2.840.10008.5.1.4.1.1.104.1'] },
          { tag: '00080018', vr: 'UI', value: 'OID' },
        ],
        tagBulkDataURI: '00420011',
        transferSyntax: '1.2.840.10008.1.2.4.50',
      },
      tagVideo: {
        necessaryTag: [
          '00100010',
          '00100020',
          '00100030',
          '00100040',
          '0020000D',
          '00080020',
          '00080030',
          '00080090',
          '00200010',
          '00080050',
          '00080005',
        ],
        createTag: [
          { tag: '00080060', vr: 'CS', value: ['XC'] },
          { tag: '0020000E', vr: 'UI', value: 'OID' },
          { tag: '00200011', vr: 'IS', value: '' },
          { tag: '00080070', vr: 'LO', value: '' },
          { tag: '00200013', vr: 'IS', value: [1] },
          { tag: '00280008', vr: 'IS', value: [0] },
          { tag: '00280009', vr: 'AT', value: ['00181063'] },
          { tag: '00280002', vr: 'US', value: [0] },
          { tag: '00280004', vr: 'CS', value: ['YBR_PARTIAL_420'] },
          { tag: '00280010', vr: 'US', value: [3] },
          { tag: '00280011', vr: 'US', value: [3] },
          { tag: '00280100', vr: 'US', value: [8] },
          { tag: '00280101', vr: 'US', value: [8] },
          { tag: '00280102', vr: 'US', value: [7] },
          { tag: '00280103', vr: 'US', value: [0] },
          { tag: '00280006', vr: 'US', value: [0] },
          { tag: '00400555', vr: 'SQ', value: '' },
          { tag: '00080008', vr: 'CS', value: ['ORIGINAL', 'PRIMARY'] },
          { tag: '00080016', vr: 'UI', value: ['1.2.840.10008.5.1.4.1.1.77.1.4.1'] },
          { tag: '00080018', vr: 'UI', value: 'OID' },
          { tag: '7FE00010', vr: 'OB', value: 'BulkDataURI' },
        ],
        tagBulkDataURI: '7FE00010',
        transferSyntax: '1.2.840.10008.1.2.4.102',
      },
      tagStudiesUID: '0020000D',
      tagSeriesUID: '0020000E',
      tagSOPUID: '00080018',
      modelBulkDataUri: 'http://kheops/dcm4chee-arc/aets/DCM4CHEE/rs',
    };
  },
  methods: {
    dicomize(study, file, dicomValue) {
      return new Promise((resolve, reject) => {
        if (study !== undefined) {
          const tags = this.getTags(file.type);
          const boundary = 'myboundary';
          const contentTypeHeader = `Content-Type: application/dicom+json; transfer-syntax=${tags.transferSyntax}`;
          if (tags !== -1) {
            const objDicom = this.generateDicom(tags.necessaryTag, tags.createTag, tags.tagBulkDataURI, study, dicomValue);
            this.generateMultiPart(boundary, [objDicom.ordered], contentTypeHeader, file, objDicom.bulkDataUri)
              .then((res) => {
                resolve(res);
              }).catch(err => {
                reject(err)
              });
          }
        } else {
          reject(new Error('Study undefined'))
        }
      })
    },
    getStudy(studyUID) {
      return new Promise((resolve, reject) => {
        HTTP.get(`/studies?StudyInstanceUID=${studyUID}`).then((res) => {
          resolve(res);
        }).catch((err) => {
          reject(err);
        });
      });
    },
    getTags(type) {
      if (type.includes('pdf')) {
        return this.tagEncapsulatedPDF;
      }
      if (type.includes('image')) {
        return this.tagPhotographicImage;
      }
      if (type.includes('video')) {
        return this.tagVideo;
      }
      return -1;
    },
    generateDicom(necessaryTag, createTag, tagBulkDataURI, study, dicomValue) {
      const dicomGenerated = {};

      let bulkDataUri = this.modelBulkDataUri;
      const oidSeries = this.generateOID();
      const oidSOP = this.generateOID();
      bulkDataUri = `${bulkDataUri}/studies/${study[this.tagStudiesUID].Value[0]}/series/${oidSeries}/instances/${oidSOP}`;

      necessaryTag.forEach((currentTag) => {
        const value = study[currentTag];
        if (value !== undefined) {
          dicomGenerated[currentTag] = value;
        }
      });
      createTag.forEach((value) => {
        if (value.value !== '') {
          if (value.tag === this.tagSeriesUID) {
            dicomGenerated[value.tag] = {
              vr: value.vr,
              Value: [oidSeries],
            };
          } else if (value.tag === this.tagSOPUID) {
            dicomGenerated[value.tag] = {
              vr: value.vr,
              Value: [oidSOP],
            };
          } else if (value.tag === tagBulkDataURI) {
            dicomGenerated[value.tag] = {
              vr: value.vr,
              BulkDataURI: `${bulkDataUri}`,
            };
          } else {
            dicomGenerated[value.tag] = {
              vr: value.vr,
              Value: value.value,
            };
          }
        }
      });
      for (const key in dicomValue) {
        if (dicomValue[key].value !== '') {
          dicomGenerated[dicomValue[key].tag] = {
            vr: dicomValue[key].vr,
            Value: [dicomValue[key].value],
          };
        }
      }

      const ordered = {};
      Object.keys(dicomGenerated).sort().forEach((key) => {
        ordered[key] = dicomGenerated[key];
      });

      return { ordered, bulkDataUri };
    },
    generateOID() {
      const oidHex = uuid.v4().split('-').join('');
      const oidDec = this.h2d(oidHex);
      return `2.25.${oidDec}`;
    },
    // https://stackoverflow.com/questions/12532871/how-to-convert-a-very-large-hex-number-to-decimal-in-javascript
    h2d(s) {
      function add(x, y) {
        let c = 0;
        const r = [];
        x = x.split('').map(Number);
        y = y.split('').map(Number);
        while (x.length || y.length) {
          const s = (x.pop() || 0) + (y.pop() || 0) + c;
          r.unshift(s < 10 ? s : s - 10);
          c = s < 10 ? 0 : 1;
        }
        if (c) r.unshift(c);
        return r.join('');
      }

      let dec = '0';
      s.split('').forEach((chr) => {
        const n = parseInt(chr, 16);
        for (let t = 8; t; t >>= 1) {
          dec = add(dec, dec);
          if (n & t) dec = add(dec, '1');
        }
      });
      return dec;
    },
    generateMultiPart(boundary, dicomHeader, contentTypeHeader, dataToPost, bulkDataUri) {
      return new Promise((resolve, reject) => {
        const header = Buffer.from(`\r\n--${boundary}\r\n${contentTypeHeader}\r\n\r\n${JSON.stringify(dicomHeader)}\r\n`, 'utf-8');
        const end = Buffer.from(`\r\n--${boundary}--`, 'utf-8');
        const file = dataToPost.content;
        let value = Buffer.from(`\r\n--${boundary}\r\nContent-Type: ${dataToPost.type}\r\nContent-Location: ${bulkDataUri}\r\n\r\n`, 'utf-8');
        this.readAsArrayBufferFile(file).then((res) => {
          value = Buffer.concat([value, Buffer.from(res)]);
          const multiPartData = Buffer.concat([header, value, end]);
          resolve(multiPartData);
        }).catch((err) => {
          reject(err)
        });
      });
    },
    // https://developer.mozilla.org/en-US/docs/Web/API/FileReader/readAsArrayBuffer
    readAsArrayBufferFile(file) {
      return new Promise((resolve) => {
        const fr = new FileReader();
        fr.onload = function () {
          const data = fr.result;
          const arrayBuffer = new Uint8Array(data);
          resolve(arrayBuffer);
        };
        fr.readAsArrayBuffer(file);
      });
    },
  },
};
export default {
  translateDICOM(studies) {
    const translate = [];
    studies.forEach((study) => {
      const objStudy = {};
      for (const dicomID in study) {
        if (dicom.dicom2name[dicomID] !== undefined) {
          objStudy[dicom.dicom2name[dicomID]] = study[dicomID];
        } else if (customdicom.customdicom2name[dicomID] !== undefined) {
          objStudy[customdicom.customdicom2name[dicomID]] = study[dicomID];
        } else {
          objStudy[dicomID] = study[dicomID];
        }
      }
      translate.push(objStudy);
    });
    return translate;
  },
  translateObjectDICOM(studies, TagID) {
    const translate = {};
    const id = (dicom.dicom2name[TagID] !== undefined ? dicom.dicom2name[TagID] : TagID);
    studies.forEach((study) => {
      const objDicom = {};
      for (const dicomID in study) {
        if (dicom.dicom2name[dicomID] !== undefined) {
          objDicom[dicom.dicom2name[dicomID]] = study[dicomID];
        } else if (customdicom.customdicom2name[dicomID] !== undefined) {
          objDicom[customdicom.customdicom2name[dicomID]] = study[dicomID];
        } else {
          objDicom[dicomID] = study[dicomID];
        }
      }
      if (objDicom[id] === undefined) {
        throw 'Your tagID is not present in all studies';
      } else if (translate[objDicom[id].Value[0]] !== undefined) {
        throw 'Your tagID is not unique';
      } else {
        translate[objDicom[id].Value[0]] = objDicom;
      }
    });
    return translate;
  },
};
